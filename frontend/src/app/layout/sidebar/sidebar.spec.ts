import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { signal } from '@angular/core';

import { Sidebar } from './sidebar';
import { AuthService } from '../../core/auth/auth.service';

const mockAuthService = {
  isAuthenticated: signal(true),
  roles: signal(['HR_ADMIN']),
  isAdmin: signal(true),
  isRecruiter: signal(false),
  isViewer: signal(false),
  hasRole: () => true,
  hasAnyRole: () => true,
  hasAllRoles: () => true,
};

describe('Sidebar', () => {
  let component: Sidebar;
  let fixture: ComponentFixture<Sidebar>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Sidebar],
      providers: [provideRouter([]), { provide: AuthService, useValue: mockAuthService }],
    }).compileComponents();

    fixture = TestBed.createComponent(Sidebar);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
