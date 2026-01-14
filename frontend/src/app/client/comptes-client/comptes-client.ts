import { ChangeDetectorRef, Component, inject, OnInit } from '@angular/core';
import { LoginService } from '../../services/login/login-service';
import { DashboardService } from '../../services/dashboard-service';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-comptes-client',
  imports: [CommonModule, RouterLink, FormsModule],
  templateUrl: './comptes-client.html',
  styleUrl: './comptes-client.scss',
})
export class ComptesClient implements OnInit {
  
  private loginService = inject(LoginService);
  private dashboardService = inject(DashboardService);
  cdr = inject(ChangeDetectorRef);
  
  comptes: any[] = [];
  loading = true;
  error = '';
  recherche = '';

  ngOnInit(): void {
    this.chargerComptes();
  }

  chargerComptes(): void {
    this.loading = true;
    this.error = '';
    
    setTimeout(() => {
      const user = this.loginService.user();
      
      if (!user) {
        this.error = 'Utilisateur non connecté';
        this.loading = false;
        this.cdr.detectChanges();
        return;
      }
      
      if (!user.clientId) {
        this.error = 'ID client non trouvé';
        this.loading = false;
        this.cdr.detectChanges();
        return;
      }
      
      this.dashboardService.getComptesClient(user.clientId).subscribe({
        next: (comptes) => {
          this.comptes = comptes;
          this.loading = false;
          this.cdr.detectChanges();
          console.log('✅ Comptes chargés:', this.comptes);
        },
        error: (err) => {
          this.error = err.error?.message || 'Erreur lors du chargement des comptes';
          this.loading = false;
          this.cdr.detectChanges();
          console.error('❌ Erreur:', err);
        }
      });
    }, 1000);
  }

  // Filtre les comptes selon la recherche
  get comptesFiltres(): any[] {
    if (!this.recherche.trim()) {
      return this.comptes;
    }
    
    const rechercheLower = this.recherche.toLowerCase();
    return this.comptes.filter(compte =>
      (compte.numeroCompte?.toLowerCase().includes(rechercheLower)) ||
      (compte.typeCompte?.toLowerCase().includes(rechercheLower)) ||
      (compte.solde?.toString().includes(rechercheLower))
    );
  }

  // Formate le type de compte
  getTypeBadgeClass(type: string): string {
    return type === 'COURANT' ? 'type-badge courant-badge' : 'type-badge epargne-badge';
  }

  // Formate le solde
  getSoldeClass(solde: number): string {
    return solde >= 0 ? 'solde-positive' : 'solde-negative';
  }

  // Action dépôt
  faireDepot(compte: any): void {
    console.log('Dépôt sur le compte:', compte.numeroCompte);
    // Implémente la logique de dépôt
  }

  // Action retrait
  faireRetrait(compte: any): void {
    console.log('Retrait sur le compte:', compte.numeroCompte);
    // Implémente la logique de retrait
  }

  // Action virement
  faireVirement(compte: any): void {
    console.log('Virement depuis le compte:', compte.numeroCompte);
    // Implémente la logique de virement
  }

  // Rafraîchir les comptes
  rafraichir(): void {
    this.chargerComptes();
  }
}