import json
from email.mime import multipart
from email.mime import text
import email.parser
import sys


def send_response_data_payload(json_payload):
    m = multipart.MIMEMultipart("form-data")
    data = text.MIMEText("response_payload", "json", "utf8")
    data.set_payload(json.JSONEncoder().encode(json_payload))
    m.attach(data)
    print("BEGIN_EXTRA_PAYLOAD")
    print(m.as_string())
    print("END_EXTRA_PAYLOAD")


def send_response_err_msg(ret_err_msg):
    print("BEGIN_EXTRA_RET_ERR_MSG")
    print(ret_err_msg)
    print("END_EXTRA_RET_ERR_MSG")


def send_response_err_msg_and_exit(ret_err_msg, code=1):
    print("BEGIN_EXTRA_RET_ERR_MSG")
    print(ret_err_msg)
    print("END_EXTRA_RET_ERR_MSG")
    sys.exit(code)
