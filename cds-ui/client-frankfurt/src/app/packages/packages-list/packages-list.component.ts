import {Component, OnInit} from '@angular/core';
import {PackagesListService} from './packages-list.service';
import {BlueprintModel} from './model/BluePrint.model';
import {Observable} from 'rxjs';

@Component({
    selector: 'app-packages-list',
    templateUrl: './packages-list.component.html',
    styleUrls: ['./packages-list.component.css']
})
export class PackagesListComponent implements OnInit {
    packages: BlueprintModel[] = [];
    private bluePrintModel: BlueprintModel;

    constructor(private packagesListService: PackagesListService) {
    }

    ngOnInit() {
        this.getAllPackages();
    }

    getAllPackages() {
        this.packagesListService.getAllPackages().subscribe(data => {
            data.forEach(element => {
                this.bluePrintModel = new BlueprintModel(
                    element.blueprintModel.id,
                    element.blueprintModel.artifactUUId,
                    element.blueprintModel.artifactType,
                    element.blueprintModel.artifactVersion,
                    element.blueprintModel.artifactDescription,
                    element.blueprintModel.internalVersion,
                    element.blueprintModel.createdDate,
                    element.blueprintModel.artifactName,
                    element.blueprintModel.published,
                    element.blueprintModel.updatedBy,
                    element.blueprintModel.tags
                );
                console.log(this.bluePrintModel.artifactName);
                this.packages.push(this.bluePrintModel);
            });


        }, error => {
            window.alert('Catalog not matching the search tag' + error);
        });
        /*.subscribe(data => {
            data.forEach(element => {
                this.packages.push(element);
            });
            data.forEach(element => {
                console.log(element);
              /*  element.map((item: any) => new BlueprintModel(
                    item.id,
                    item.artifactUUId,
                    item.artifactType,
                    item.artifactVersion,
                    item.artifactDescription,
                    item.internalVersion,
                    item.createdDate,
                    item.artifactName,
                    item.published,
                    item.updatedBy,
                    item.tags
                ));*/
        /*  });

      }, error => {
          console.log(error);
      });*/
    }


}
