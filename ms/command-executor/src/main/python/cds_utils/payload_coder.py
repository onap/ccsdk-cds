import json
from email.mime import multipart
from email.mime import text
import email.parser

def send_response_data_payload(json_payload):
    m = multipart.MIMEMultipart("form-data")
    data = text.MIMEText("response_payload", "json", "utf8")
    data.set_payload(json.JSONEncoder().encode(json_payload))
    m.attach(data)
    print("BEGIN_EXTRA_PAYLOAD")
    print(m.as_string())
    print("END_EXTRA_PAYLOAD")