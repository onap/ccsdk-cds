
## Install virtual environments

```bash
python3 -m pip install --user virtualenv # Install virtualenv

python3 -m venv venv # Create virtual environment under py-executor path

source env/bin/activate # Activate the current virtualenv

pip install -r requirements.txt # Install the pip packages defined in requirements.txt file

```
## Generate TLS certificates

```bash
# Generate Server Certificates( chain and private keys ) using config file
openssl req -config py-executor.conf -new -x509 -newkey rsa:4096 -nodes -keyout py-executor-key.pem -days 3650 -out py-executor-chain.pem

# Verify the certificates generated
openssl x509 -in py-executor-chain.pem -text -noout
```

## Generate Python GRPC bindings from Proto file

```bash
cd blueprints_grpc/proto

# Remove Python generated files
rm *pb2*.py

#Generate proto python
python3 -m grpc_tools.protoc -I. --python_out=. --grpc_python_out=. *.proto
```
fix the python import package issues.
