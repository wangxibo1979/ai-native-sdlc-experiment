import json
from pathlib import Path

contract = json.loads((Path(__file__).parent / 'contract/payment-api.json').read_text(encoding='utf-8-sig'))
assert set(contract['request']) == {'orderId', 'amountFen', 'currency'}, 'consumer request fields do not match payment contract'
assert contract['response'] == {'paymentId': 'string'}, 'consumer response does not match payment contract'
print('PASS consumer contract field check')
