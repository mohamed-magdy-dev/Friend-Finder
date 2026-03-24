import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FriendRequests } from './friend-requests';

describe('FriendRequests', () => {
  let component: FriendRequests;
  let fixture: ComponentFixture<FriendRequests>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FriendRequests]
    })
    .compileComponents();

    fixture = TestBed.createComponent(FriendRequests);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
