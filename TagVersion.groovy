/*-
 * ============LICENSE_START=======================================================
 * ONAP CCSDK
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights
 *                             reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END============================================
 * ===================================================================
 *
 */

package org.onap.ccsdk.distribution


def versionArray;
if ( project.properties['ccsdk.project.version'] != null ) {
	versionArray = project.properties['ccsdk.project.version'].split('\\.');
}

if ( project.properties['ccsdk.project.version'].endsWith("-SNAPSHOT") ) {
	patchArray = versionArray[2].split('-');
	project.properties['project.docker.latestminortag.version']=versionArray[0] + '.' + versionArray[1] + "-SNAPSHOT-latest";
	project.properties['project.docker.latestfulltag.version']=versionArray[0] + '.' + versionArray[1] + '.' + patchArray[0] + "-SNAPSHOT-latest";
	project.properties['project.docker.latesttagtimestamp.version']=versionArray[0] + '.' + versionArray[1] + '.' + patchArray[0] + "-SNAPSHOT-"+project.properties['ccsdk.build.timestamp'];
} else {
	project.properties['project.docker.latestminortag.version']=versionArray[0] + '.' + versionArray[1] + "-STAGING-latest";
	project.properties['project.docker.latestfulltag.version']=versionArray[0] + '.' + versionArray[1] + '.' + versionArray[2] + "-STAGING-latest";
	project.properties['project.docker.latesttagtimestamp.version']=versionArray[0] + '.' + versionArray[1] + '.' + versionArray[2] + "-STAGING-"+project.properties['ccsdk.build.timestamp'];
}
