/*
* ============LICENSE_START=======================================================
* ONAP : CDS
* ================================================================================
* Copyright (C) 2026 Deutsche Telekom AG.
*=================================================================================
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
* ============LICENSE_END=========================================================
*/

import { Component, ElementRef, EventEmitter, HostListener, Input, Output, ViewChild } from '@angular/core';

type HeaderMenu = 'help' | 'user';

@Component({
  selector: 'app-header-actions',
  templateUrl: './header-actions.component.html'
})
export class HeaderActionsComponent {

  @Input() showGettingStarted = false;
  @Output() gettingStarted = new EventEmitter<void>();

  @ViewChild('helpMenu', { static: true }) helpMenu: ElementRef<HTMLElement>;
  @ViewChild('userMenu', { static: true }) userMenu: ElementRef<HTMLElement>;
  @ViewChild('helpToggle', { static: true }) helpToggle: ElementRef<HTMLButtonElement>;
  @ViewChild('userToggle', { static: true }) userToggle: ElementRef<HTMLButtonElement>;

  openMenu: HeaderMenu = null;

  toggle(menu: HeaderMenu) {
    this.openMenu = this.openMenu === menu ? null : menu;
  }

  startGettingStarted() {
    this.openMenu = null;
    this.gettingStarted.emit();
  }

  @HostListener('keydown.escape')
  closeAndRestoreFocus() {
    if (this.openMenu) {
      const toggle = this.openMenu === 'help' ? this.helpToggle : this.userToggle;
      this.openMenu = null;
      toggle.nativeElement.focus();
    }
  }

  @HostListener('document:click', ['$event.target'])
  @HostListener('document:focusin', ['$event.target'])
  closeWhenOutside(target: Node) {
    const openDropdown = this.openMenu === 'help' ? this.helpMenu : this.userMenu;
    if (this.openMenu && !openDropdown.nativeElement.contains(target)) {
      this.openMenu = null;
    }
  }
}
