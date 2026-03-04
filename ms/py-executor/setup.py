#!/usr/bin/env python
"""Copyright 2019 AT&T Intellectual Property.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
"""

from setuptools import setup, find_packages

setup(
    name="py-executor",
    version="0.1",
    description="CDS Python Executor",
    packages=find_packages(exclude=["test", "test.*"]),
    install_requires=[
        "grpcio>=1.48.2",
        "grpcio-tools>=1.48.2",
        "protobuf>=3.20.3",
        "configparser>=7.1.0",
        "requests>=2.31.0",
        "ncclient>=0.6.15",
        "ansible-core>=2.14.17",
        "opentelemetry-distro==0.44b0",
        "opentelemetry-exporter-otlp-proto-http==1.23.0",
    ],
)
