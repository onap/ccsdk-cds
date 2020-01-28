import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {ScriptsTabComponent} from './scripts-tab.component';

describe('ScriptsTabComponent', () => {
    let component: ScriptsTabComponent;
    let fixture: ComponentFixture<ScriptsTabComponent>;

    beforeEach(async(() => {
        TestBed.configureTestingModule({
            declarations: [ScriptsTabComponent]
        })
            .compileComponents();
    }));

    beforeEach(() => {
        fixture = TestBed.createComponent(ScriptsTabComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
