/*
============LICENSE_START==========================================
===================================================================
Copyright (C) 2019 IBM Intellectual propertyNameerty. All rights reserved.
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

import { Pipe, PipeTransform } from '@angular/core';
  
@Pipe({
  name: 'sort',
  pure:false,
})
export class SortPipe implements PipeTransform {
  
  transform(arrayData: any[], direcion: string, propertyName?: string): any {
    if (!arrayData) {
      return [];
    }
    if (!direcion || !propertyName) {
      return arrayData
    }
    if (arrayData.length > 0) {
      const _direction = direcion === 'asc' ? -1 : 1,
        isArray = Array.isArray(arrayData),
        arrayDataType = typeof arrayData[0],
        flag = isArray && arrayDataType === 'object' ? true : isArray && arrayDataType !== 'object' ? false : true;
      arrayData.sort((a, b) => {
        a = flag ? a[propertyName] : a;
        b = flag ? b[propertyName] : b;
        if (typeof a === 'string') {
          return a.toLowerCase() > b.toLowerCase() ? -1 * _direction : 1 * _direction;
        } else if (typeof a === 'number') {
          return a - b > 0 ? -1 * _direction : 1 * _direction;
        }
      });
    }
    return arrayData;
  }
}