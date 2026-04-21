/*
============LICENSE_START==========================================
===================================================================
Copyright (C) 2019 Orange. All rights reserved.
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

export function mapBlueprintError(error: any): { title: string; suggestion: string } {
    const body: string = (error && error.error && error.error.message) || (error && error.message) || '';
    const status: number = error && error.status;
    if (status === 409 || body.includes('already exists')) {
        return {
            title: 'Package already exists',
            suggestion: 'Increment the version number in the Metadata tab (e.g. 1.0.1).'
        };
    }
    if (body.includes('does not exist in system') || body.includes('validation')) {
        return {
            title: 'Blueprint validation failed',
            suggestion: 'Check the DSL Properties and Template Mapping tabs for missing or malformed entries.'
        };
    }
    if (status === 401 || body.includes('UNAUTHENTICATED')) {
        return {
            title: 'Session expired',
            suggestion: 'Refresh the page and log in again.'
        };
    }
    if (status === 413) {
        return {
            title: 'Package too large',
            suggestion: 'Consider splitting large script files into separate uploads.'
        };
    }
    return {
        title: 'Save failed',
        suggestion: 'Verify all required Metadata fields are complete. Check the browser console for details.'
    };
}
