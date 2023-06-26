/*
============LICENSE_START==========================================
===================================================================
Copyright (C) 2018-19 IBM Intellectual Property. All rights reserved.
===================================================================

Unless otherwise specified, all software contained herein is licensed
under the Apache License, Version 2.0 (the License);
you may not use this software except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
============LICENSE_END============================================
*/

const application = require('./dist');
const fs = require('fs')

module.exports = application;

if (require.main === module) {

  try {
    var p12File = process.env.KEYSTORE || "aaf.p12"
    var passwdFile = process.env.PASSPHRASE || ".enc"

    var data = fs.readFileSync(passwdFile, 'utf8')
    var elements = data.match(/cadi_keystore_password_p12=(.*)\n/)
    var passphrase = elements[1]
    var p12 = fs.readFileSync(p12File)
  } catch(e){
    console.error('Reading keystore error :', e)
    process.exit(11)
  }

  // Run the application
  const config = {
    rest: {
      protocol: process.env.PROTOCOL || 'https',
      pfx: p12,
      passphrase: passphrase,
      port: +process.env.PORT || 3000,
      host: process.env.HOST || 'localhost',
      openApiSpec: {
        // useful when used with OASGraph to locate your application
        setServersFromRequest: true,
      },
    },
  };
  application.main(config).catch(err => {
    console.error('Cannot start the application.', err);
    process.exit(1);
  });
}
