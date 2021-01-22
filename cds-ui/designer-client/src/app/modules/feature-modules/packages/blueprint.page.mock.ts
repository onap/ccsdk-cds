import {BlueprintPage} from './model/Blueprint.model';
import {PackagesDashboardState} from './model/packages-dashboard.state';

export function getBlueprintPageMock(): BlueprintPage {
    return {
        content: [
            {
                id: 'bc0dabea-3112-4202-a4b9-6a525bcc19a9',
                artifactUUId: null,
                artifactType: 'SDNC_MODEL',
                artifactVersion: '1.0.0',
                artifactDescription: 'Controller Blueprint for vLB_CDS123:1.0.0',
                internalVersion: null,
                createdDate: '2019-10-30T13:55:16.000Z',
                artifactName: 'vLB_CDS123',
                published: 'N',
                updatedBy: 'Abdelmuhaimen Seaudi',
                tags: 'test, vDNS-CDS, SCALE-OUT, MARCO'
            },
            {
                id: 'a741913f-2b1b-4eb8-94b3-8c6b08928f0a',
                artifactUUId: null,
                artifactType: 'SDNC_MODEL',
                artifactVersion: '1.0.0',
                artifactDescription: 'Controller Blueprint for vLB_CDS12312312:1.0.0',
                internalVersion: null,
                createdDate: '2019-10-30T14:58:04.000Z',
                artifactName: 'vLB_CDS12312312',
                published: 'N',
                updatedBy: 'Abdelmuhaimen Seaudi',
                tags: 'test, vDNS-CDS, SCALE-OUT, MARCO'
            }
        ],
        pageable: {
            sort: {
                sorted: true,
                unsorted: false,
                empty: false
            },
            offset: 0,
            pageSize: 2,
            pageNumber: 0,
            paged: true,
            unpaged: false
        },
        last: false,
        totalElements: 4,
        totalPages: 2,
        first: true,
        empty: false
    };

}

