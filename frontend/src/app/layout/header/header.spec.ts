import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { signal } from '@angular/core';

import { Subject } from 'rxjs';

import { Header } from './header';
import { AuthService } from '../../core/auth/auth.service';

const mockAuthService = {
  profileUpdated: new Subject<void>(),
  isAuthenticated: signal(true),
  roles: signal(['HR_ADMIN']),
  currentUser: signal({
    sub: 'test-sub-123',
    firstName: 'Test',
    lastName: 'User',
    fullName: 'Test User',
    email: 'test@example.com',
    preferredUsername: 'testuser',
    roles: ['HR_ADMIN'],
  }),
  isAdmin: signal(true),
  isRecruiter: signal(false),
  isViewer: signal(false),
  hasRole: () => true,
  hasAnyRole: () => true,
  hasAllRoles: () => true,
  getUserProfile: () => ({
    sub: 'test-sub-123',
    firstName: 'Test',
    lastName: 'User',
    fullName: 'Test User',
    email: 'test@example.com',
    preferredUsername: 'testuser',
    roles: ['HR_ADMIN'],
  }),
  logout: async () => {},
  manageAccount: () => {},
  changePassword: () => {},
};

describe('Header', () => {
  let component: Header;
  let fixture: ComponentFixture<Header>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Header],
      providers: [provideRouter([]), { provide: AuthService, useValue: mockAuthService }],
    }).compileComponents();

    fixture = TestBed.createComponent(Header);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
