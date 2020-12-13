import {Component, OnDestroy, OnInit} from '@angular/core';
import {distinctUntilChanged, takeUntil} from 'rxjs/operators';
import {DesignerStore} from '../../designer/designer.store';
import {Subject} from 'rxjs';
import {PackageCreationUtils} from '../package-creation.utils';
import {PackageCreationStore} from '../package-creation.store';

@Component({
    selector: 'app-topology-template',
    templateUrl: './topology-template.component.html',
    styleUrls: ['./topology-template.component.css']
})
export class TopologyTemplateComponent implements OnInit, OnDestroy {

    ngUnsubscribe = new Subject();
    content = ' ';
    private cbaPackage: any;
    private designerState: any;

    constructor(private designerStore: DesignerStore,
                private packageCreationUtils: PackageCreationUtils,
                private packageCreationStore: PackageCreationStore) {

        this.packageCreationStore.state$
            .pipe(distinctUntilChanged((a: any, b: any) => JSON.stringify(a) === JSON.stringify(b)),
                takeUntil(this.ngUnsubscribe))
            .subscribe(
                cbaPackage => {
                    this.cbaPackage = cbaPackage;
                });
        this.designerStore.state$.pipe(
            distinctUntilChanged((a: any, b: any) => JSON.stringify(a) === JSON.stringify(b)),
            takeUntil(this.ngUnsubscribe))
            .subscribe(state => {
                this.designerState = state;
                this.content =
                    this.packageCreationUtils.transformToJson(state.template);
                console.log(this.content);
                console.log(state.template);
            });
    }

    ngOnInit() {
    }

    textChanged($event: {}) {
        this.cbaPackage.templateTopology.content = this.content;
        this.designerState.template = JSON.parse(this.content);

    }

    ngOnDestroy() {
        this.ngUnsubscribe.next();
        this.ngUnsubscribe.complete();
    }
}
