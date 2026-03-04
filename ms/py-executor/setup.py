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
        "grpcio>=1.25.0",
        "grpcio-tools>=1.25.0",
        "protobuf>=3.20.1",
        "configparser>=4.0.2",
        "requests>=2.22.0",
        "ncclient>=0.6.6",
        "ansible>=2.8.5",
    ],
)
