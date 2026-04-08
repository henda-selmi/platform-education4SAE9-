import { Injectable, inject } from '@angular/core';
import { AuthService, User } from '@core/authentication';
import { NgxPermissionsService, NgxRolesService } from 'ngx-permissions';
import { switchMap, tap } from 'rxjs';
import { Menu, MenuService } from './menu.service';

@Injectable({
  providedIn: 'root',
})
export class StartupService {
  private readonly authService = inject(AuthService);
  private readonly menuService = inject(MenuService);
  private readonly permissonsService = inject(NgxPermissionsService);
  private readonly rolesService = inject(NgxRolesService);

  /**
   * Load the application only after get the menu or other essential informations
   * such as permissions and roles.
   */
  load() {
    return new Promise<void>((resolve, reject) => {
      this.authService
        .change()
        .pipe(
          tap(user => this.setPermissions(user)),
          switchMap(() => this.authService.menu()),
          tap(menu => this.setMenu(menu))
        )
        .subscribe({
          next: () => resolve(),
          error: () => resolve(),
        });
    });
  }

  private setMenu(menu: Menu[]) {
    this.menuService.addNamespace(menu, 'menu');
    this.menuService.set(menu);
  }

  private setPermissions(user: User) {
    const isAdmin = user.roles?.includes('ADMIN');
    const permissions: string[] = isAdmin
      ? ['canRead', 'canAdd', 'canEdit', 'canDelete']
      : ['canRead', 'canAdd'];

    this.permissonsService.loadPermissions(permissions);
    this.rolesService.flushRoles();

    if (isAdmin) {
      this.rolesService.addRoles({ ADMIN: permissions });
    } else {
      this.rolesService.addRoles({ STUDENT: permissions });
    }
  }
}
