import { Injectable } from '@angular/core';

import { Observable, of, BehaviorSubject } from 'rxjs';

@Injectable({
    providedIn: 'root'
})
export class SharedService {

    // based on edit Mode, edit=false
    mode = new BehaviorSubject(false);
    list = new BehaviorSubject('');
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

    // from file from tempplate&mapping list
    deleteFromList(filename) {
        this.list.next(filename);
    }
    listAction(): Observable<string> {
        return this.list.asObservable();
    }

}
