import { Component, OnInit, OnDestroy } from '@angular/core';
import { DesignerStore } from '../designer.store';
import { PackageCreationUtils } from '../../package-creation/package-creation.utils';
import { RouterLink, Router } from '@angular/router';
import { Subject } from 'rxjs';

@Component({
    selector: 'app-designer-source-view',
    templateUrl: './source-view.component.html',
    styleUrls: ['./source-view.component.css']
})
export class DesignerSourceViewComponent implements OnInit, OnDestroy {

    content = '';
    lang = 'json';
    private controllerSideBar: boolean;
    private ngUnsubscribe = new Subject();

    constructor(private store: DesignerStore,
                private packageCreationUtils: PackageCreationUtils,
                private router: Router) {
        this.controllerSideBar = true;
    }

    ngOnInit() {
        this.store.state$.subscribe(
            state => {
                console.log(state);
                this.content = this.packageCreationUtils.transformToJson(state.template);
            });

    }

    convertAndOpenInDesingerView() {
        // TODO validate json against scheme
        console.log('convertAndOpenInDesingerView ...', this.content);
        this.store.saveSourceContent(this.content);
        this.router.navigateByUrl('/packages/designer');
    }

    ngOnDestroy() {
        this.ngUnsubscribe.next();
        this.ngUnsubscribe.complete();
    }
}
