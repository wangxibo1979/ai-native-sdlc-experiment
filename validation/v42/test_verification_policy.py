"""Regression against the confirmed acceptance rules, independent of coverage."""
import json
import pathlib
import unittest

ROOT = pathlib.Path(__file__).resolve().parent


def read(name):
    return json.loads((ROOT / name).read_text(encoding='utf-8-sig'))


class MandatoryAcceptancePolicy(unittest.TestCase):
    def test_full_map_keeps_all_confirmed_business_rules_blocking(self):
        rows = read('outputs/test_map_full.json')
        self.assertEqual(20, len(rows))
        self.assertEqual(['YES'] * 20, [r['是否阻断'] for r in rows],
                         'Coverage success cannot waive mandatory acceptance')

    def test_legacy_map_uses_the_same_business_importance(self):
        rows = read('outputs/test_map_legacy.json')
        self.assertEqual(['YES'] * 20, [r['是否阻断'] for r in rows],
                         'Requiredness cannot change with the selected suite')

    def test_contract_requires_the_complete_confirmed_target_set(self):
        self.assertEqual(['T%02d' % i for i in range(1, 21)],
                         read('verification_contract.json').get('required_targets'),
                         'The entry must enforce the reviewed blocking policy')


if __name__ == '__main__':
    unittest.main()
