import { Injectable } from '@angular/core';

import { Observable, of, BehaviorSubject } from 'rxjs';

@Injectable({
    providedIn: 'root'
})
export class SharedService {

    // based on edit Mode, edit=false
    mode = new BehaviorSubject(false);
    constructor() {
    }

    isEdit(): Observable<boolean> {
        return this.mode.asObservable();
    }
    enableEdit() {
        this.mode.next(true);
    }
    disableEdit() {
        this.mode.next(false);
    }

}
