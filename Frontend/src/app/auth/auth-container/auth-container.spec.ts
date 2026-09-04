import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AuthContainer } from './auth-container';

describe('AuthContainer', () => {
  let component: AuthContainer;
  let fixture: ComponentFixture<AuthContainer>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AuthContainer]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AuthContainer);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
