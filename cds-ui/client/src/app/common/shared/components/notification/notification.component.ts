/*
============LICENSE_START==========================================
===================================================================
Copyright (C) 2019 IBM Intellectual Property. All rights reserved.
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

import { Component, OnInit, Input } from '@angular/core';

import { Notification, NotificationType } from './notification';
import { NotificationService } from './notification.service';

@Component({
  selector: 'app-notification',
  templateUrl: './notification.component.html',
  styleUrls: ['./notification.component.scss']
})
export class NotificationComponent implements OnInit {

  @Input() id: string;

  alerts: Notification[] = [];

  constructor(private alertService: NotificationService) { }

  ngOnInit() {
      this.alertService.getAlert(this.id).subscribe((alert: Notification) => {
          if (!alert.message) {
              this.alerts = [];
              return;
          }
          this.alerts.push(alert);
      });
  }

  
  cssStyles(alert: Notification) {
    if (!alert) {
        return;
    }
    switch (alert.type) {
        case NotificationType.Success:
            return 'alert alert-success';
        case NotificationType.Error:
            return 'alert alert-danger';
        case NotificationType.Info:
            return 'alert alert-info';
        case NotificationType.Warning:
            return 'alert alert-warning';
    }
}

  closeNotification(alert: Notification) {
      this.alerts = this.alerts.filter(x => x !== alert);
  }

}
