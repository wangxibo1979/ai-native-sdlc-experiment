from pathlib import Path
import json, shutil, subprocess, sys
root=Path(__file__).resolve().parents[1]
work=root/('validation/v4/work-'+(sys.argv[1] if len(sys.argv)>1 else 'fresh'))
if work.exists():raise SystemExit('Do not overwrite V4 history')
work.mkdir(parents=True)

def run(cmd,cwd=work,expected=0):
 r=subprocess.run(cmd,cwd=cwd,capture_output=True,text=True,encoding='utf-8',errors='replace',timeout=30)
 print('COMMAND',json.dumps(cmd),'EXIT',r.returncode,flush=True);print(r.stdout+r.stderr,flush=True)
 if r.returncode!=expected:raise AssertionError('unexpected command result')
 return r

def git(*args):return run(['git','-c','user.name=V41 synthetic experiment','-c','user.email=v41@example.invalid',*args])
def write(path,s):
 p=work/path;p.parent.mkdir(parents=True,exist_ok=True);p.write_text(s,encoding='utf-8')
def req(review=False,positive=False):
 states='CREATED, PAID'+(', REVIEW' if review else '')
 return '### Requirement: Payment lifecycle\nThe system SHALL support '+states+'.'+(' The system SHALL reject non-positive amounts.' if positive else '')+'\n\n#### Scenario: Accepted state\n- **WHEN** a payment uses a documented state\n- **THEN** the state is accepted\n'+('\n#### Scenario: Review\n- **WHEN** a payment uses REVIEW\n- **THEN** REVIEW is accepted\n' if review else '')+('\n#### Scenario: Invalid amount\n- **WHEN** the amount is non-positive\n- **THEN** it is rejected\n' if positive else '')

def change(name,review,positive):
 d='openspec/changes/'+name+'/'
 write(d+'.openspec.yaml','schema: spec-driven\ncreated: 2026-11-08\n')
 write(d+'proposal.md','# Proposal\n\n## Why\nThe synthetic payment lifecycle needs an additional explicit rule for this experiment.\n\n## What Changes\n- Update lifecycle behavior.\n\n## Capabilities\n### Modified Capabilities\n- `lifecycle`: update requirement\n\n## Impact\nOnly isolated lifecycle tests and specification.\n')
 write(d+'design.md','# Design\nUse standalone Java rule and full MODIFIED requirement.\n')
 write(d+'tasks.md','# Tasks\n\n- [x] 1.1 Implement and execute the isolated rule test.\n')
 write(d+'specs/lifecycle/spec.md','## MODIFIED Requirements\n\n'+req(review,positive))

git('init','-b','target')
write('openspec/config.yaml','schema: spec-driven\n')
write('openspec/specs/lifecycle/spec.md','# Lifecycle\n\n## Purpose\nDefine the allowed states and input constraints for the isolated synthetic lifecycle experiment.\n\n## Requirements\n\n'+req())
git('add','.');git('commit','-m','V4 shared initial lifecycle')
base=git('rev-parse','HEAD').stdout.strip()
git('checkout','-b','change-a');change('add-review',True,False)
write('ReviewRule.java','public class ReviewRule { public static boolean accepts(String state) {return state.equals("CREATED")||state.equals("PAID")||state.equals("REVIEW");} }\n')
write('ReviewRuleTest.java','public class ReviewRuleTest { public static void main(String[] args){if(!ReviewRule.accepts("REVIEW"))throw new AssertionError("REVIEW lost");System.out.println("PASS REVIEW rule");} }\n')
run(['javac','ReviewRule.java','ReviewRuleTest.java']);run(['java','ReviewRuleTest'])
git('add','openspec','ReviewRule.java','ReviewRuleTest.java');git('commit','-m','A adds REVIEW')
a=git('rev-parse','HEAD').stdout.strip()
git('checkout','-b','change-b',base);change('positive-amount',False,True)
write('AmountRule.java','public class AmountRule { public static void validate(long amount){if(amount<=0)throw new IllegalArgumentException("positive amount required");} }\n')
write('AmountRuleTest.java','public class AmountRuleTest {public static void main(String[] args){AmountRule.validate(100);try{AmountRule.validate(0);throw new AssertionError("zero accepted");}catch(IllegalArgumentException expected){}System.out.println("PASS amount rule");}}\n')
run(['javac','AmountRule.java','AmountRuleTest.java']);run(['java','AmountRuleTest'])
git('add','openspec','AmountRule.java','AmountRuleTest.java');git('commit','-m','B adds positive amount rule based on old spec')
b=git('rev-parse','HEAD').stdout.strip()
git('checkout','target');git('merge','--no-ff','change-a','-m','Merge A in isolated target')
run(['openspec.cmd','archive','add-review','--yes'])
git('add','openspec');git('commit','-m','Archive A')
git('merge','--no-ff','change-b','-m','Merge independent B artifacts without text conflict')
pre=git('rev-parse','HEAD').stdout.strip()
# A deliberately unsafe control runs in a copy and never replaces the safe target.
control=root/('validation/v4/unsafe-control-'+(sys.argv[1] if len(sys.argv)>1 else 'fresh'));shutil.copytree(work,control,ignore=shutil.ignore_patterns('.git','*.class'))
run(['openspec.cmd','archive','positive-amount','--yes'],cwd=control)
unsafe=(control/'openspec/specs/lifecycle/spec.md').read_text()
loss='REVIEW' not in unsafe and 'non-positive' in unsafe
print('UNSAFE_ARCHIVE_EXIT_0_REVIEW_LOST='+str(loss),flush=True)
if not loss:raise AssertionError('expected stale-delta loss was not reproduced')
# Both fixture requirements were declared before execution; reconciliation preserves both.
write('openspec/changes/positive-amount/specs/lifecycle/spec.md','## MODIFIED Requirements\n\n'+req(True,True))
run(['openspec.cmd','validate','positive-amount','--strict'])
run(['openspec.cmd','archive','positive-amount','--yes'])
final=(work/'openspec/specs/lifecycle/spec.md').read_text()
assert 'REVIEW' in final and 'non-positive' in final,'final confirmed rules lost'
run(['javac','ReviewRule.java','ReviewRuleTest.java','AmountRule.java','AmountRuleTest.java']);run(['java','ReviewRuleTest']);run(['java','AmountRuleTest'])
run(['openspec.cmd','validate','--specs','--strict','--no-interactive']);run(['openspec.cmd','validate','--archived','--no-interactive'])
git('add','openspec');git('commit','-m','Reconcile B with latest A spec and archive preserving both rules')
end=git('rev-parse','HEAD').stdout.strip()
summary={'base':base,'A':a,'B':b,'pre_reconcile':pre,'final':end,'git_text_merge_exit':0,'unsafe_archive_exit':0,'unsafe_lost_review':loss,'final_rules':['REVIEW','reject non-positive amount'],'decision':'Synthetic rules declared upfront; retain both. Real conflicting business choices require human owner.'}
(root/'outputs/evidence/v4-result.json').write_text(json.dumps(summary,indent=2),encoding='utf-8')
print('V4_ALL_EXPECTATIONS_MET')
