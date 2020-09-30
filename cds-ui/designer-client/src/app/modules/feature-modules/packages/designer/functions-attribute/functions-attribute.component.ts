import {Component, OnInit} from '@angular/core';
import {DesignerStore} from '../designer.store';
import {PackageCreationStore} from '../../package-creation/package-creation.store';
import {Subject} from 'rxjs';
import {distinctUntilChanged, takeUntil} from 'rxjs/operators';
import {CBAPackage} from '../../package-creation/mapping-models/CBAPacakge.model';

@Component({
    selector: 'app-functions-attribute',
    templateUrl: './functions-attribute.component.html',
    styleUrls: ['./functions-attribute.component.css']
})
export class FunctionsAttributeComponent implements OnInit {

    ngUnsubscribe = new Subject();
    private designerDashboardState: DecodeSuccessCallback;
    private cbaPackage: CBAPackage;

    constructor(private designerStore: DesignerStore,
                private packageCreationStore: PackageCreationStore) {
    }

    ngOnInit() {
        this.designerStore.state$
            .pipe(
                distinctUntilChanged((a: any, b: any) => JSON.stringify(a) === JSON.stringify(b)),
                takeUntil(this.ngUnsubscribe))
            .subscribe(designerDashboardState => {
                this.designerDashboardState = designerDashboardState;
            });

        this.packageCreationStore.state$
            .pipe(
                distinctUntilChanged((a: any, b: any) => JSON.stringify(a) === JSON.stringify(b)),
                takeUntil(this.ngUnsubscribe))
            .subscribe(cbaPackage => {
                this.cbaPackage = cbaPackage;
            });

    }

}
