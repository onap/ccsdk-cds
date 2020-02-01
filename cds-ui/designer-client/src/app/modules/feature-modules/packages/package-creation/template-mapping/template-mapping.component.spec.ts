import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { TemplateMappingComponent } from './template-mapping.component';
import { TemplMappCreationComponent } from './templ-mapp-creation/templ-mapp-creation.component';
import { TemplMappListingComponent } from './templ-mapp-listing/templ-mapp-listing.component';
import { By } from '@angular/platform-browser';

describe('TemplateMappingComponent', () => {
  let component: TemplateMappingComponent;
  let fixture: ComponentFixture<TemplateMappingComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [
        TemplateMappingComponent,
        TemplMappCreationComponent,
        TemplMappListingComponent
      ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TemplateMappingComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });


  it('should load create component if create flag is true', () => {
    component.creationView = true;
    fixture.detectChanges();
    const compiled = fixture.debugElement.nativeElement;

    const element = fixture.debugElement.query(By.css('app-templ-mapp-creation'));
    expect(element).toBeTruthy();
    const child: TemplMappCreationComponent = element.componentInstance;
    expect(child).not.toBeNull();

    const listElement = fixture.debugElement.query(By.css('app-templ-mapp-listing'));
    expect(listElement).toBeFalsy();

  });

  it('should load listing component by default', () => {
    const compiled = fixture.debugElement.nativeElement;

    const element = fixture.debugElement.query(By.css('app-templ-mapp-creation'));
    expect(element).toBeFalsy();

    const listElement = fixture.debugElement.query(By.css('app-templ-mapp-listing'));
    expect(listElement).toBeTruthy();
    const child: TemplMappListingComponent = listElement.componentInstance;
    expect(child).not.toBeNull();

  });
});
