export class Page<T> {
    content: T[];
    pageable: {
        sort: {
            unsorted: boolean,
            sorted: boolean,
            empty: boolean
        };

        offset: number,
        pageSize: number,
        pageNumber: number,
        paged: boolean,
        unpaged: boolean,
    };
    totalPages: number;
    totalElements: number;
    last: boolean;
    first: boolean;
    empty: boolean;
}
