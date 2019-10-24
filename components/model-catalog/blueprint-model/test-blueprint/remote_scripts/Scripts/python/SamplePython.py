#!/usr/bin/python

import sys

#Optional : import this utility class if returning payload
from cds_utils.payload_coder import send_response_data_payload

# Do your work...  using try .. except to handle errors and return False if error
print(sys.argv[1])

# Optional : return a JSON payload to the be published under the response-data output attribute
send_response_data_payload({"étudiant": ["Mélanie", "Joséphine"]})

# Always return a boolean indicating success or not..
sys.exit(True)