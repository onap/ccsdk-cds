import { Observable, BehaviorSubject } from 'rxjs';
import { Injectable } from '@angular/core';

export class Store<T> {
    state$: Observable<T>;
    private subject: BehaviorSubject<T>;

    protected constructor(initialState: T) {
        this.subject = new BehaviorSubject(initialState);
        this.state$ = this.subject.asObservable();
    }

    get state(): T {
        return this.subject.getValue();
    }

    protected setState(nextState: T): void {
        console.log('setting state', this.subject);
        this.subject.next(nextState);
        console.log('current state', this.subject);
    }

    public unsubscribe() {
        this.subject.unsubscribe();
    }

}
