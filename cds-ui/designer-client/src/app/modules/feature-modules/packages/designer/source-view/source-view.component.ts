import { Component, OnInit, OnDestroy } from '@angular/core';
import { DesignerStore } from '../designer.store';
import { PackageCreationUtils } from '../../package-creation/package-creation.utils';
import { RouterLink, Router, ActivatedRoute } from '@angular/router';
import { Subject } from 'rxjs';
import { SourceViewService } from './source.service';
import { BluePrintDetailModel } from '../../model/BluePrint.detail.model';

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
    viewedPackage: BluePrintDetailModel = new BluePrintDetailModel();

    constructor(private store: DesignerStore,
                private packageCreationUtils: PackageCreationUtils,
                private router: Router,
                private route: ActivatedRoute,
                private sourceViewService: SourceViewService) {
        this.controllerSideBar = true;
    }

    ngOnInit() {
        this.store.state$.subscribe(
            state => {
                console.log(state);
                this.content = this.packageCreationUtils.transformToJson(state.template);
            });

            const id = this.route.snapshot.paramMap.get('id');
        this.sourceViewService.getPagedPackages(id).subscribe(
            (bluePrintDetailModels) => {
                if (bluePrintDetailModels) {
                    this.viewedPackage = bluePrintDetailModels[0];
                }
            }); 

    }

    convertAndOpenInDesingerView(id) {
        // TODO validate json against scheme
        console.log('convertAndOpenInDesingerView ...', this.content);
        this.store.saveSourceContent(this.content);
        this.router.navigate(['/packages/designer', id]);
    }

    ngOnDestroy() {
        this.ngUnsubscribe.next();
        this.ngUnsubscribe.complete();
    }
}
