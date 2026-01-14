import { ChangeDetectorRef, Component, inject, OnInit } from '@angular/core';
import { LoginService } from '../../services/login/login-service';
import { DashboardService } from '../../services/dashboard-service';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-transactions-client',
  imports: [CommonModule],
  templateUrl: './transactions-client.html',
  styleUrl: './transactions-client.scss',
})
export class TransactionsClient implements OnInit {
  private loginService = inject(LoginService);
  private dashboardService = inject(DashboardService);
  cdr = inject(ChangeDetectorRef);
  
  transactions: any[] = [];
  mesComptes: any[] = []; // Stocke tous tes comptes
  
  ngOnInit(): void {
    setTimeout(() => {
      const user = this.loginService.user();
      if (user?.clientId) {
        // 1. Récupère d'abord TES comptes
        this.dashboardService.getComptesClient(user.clientId).subscribe({
          next: (comptes) => {
            this.mesComptes = comptes;
            console.log('Mes comptes:', this.mesComptes);
            
            // 2. Maintenant récupère les transactions
            this.dashboardService.getTransactionsClient(user.clientId).subscribe({
              next: (transactions) => {
                this.transactions = transactions;
                this.cdr.detectChanges(); 
                console.log('Transactions:', this.transactions);
              }
            });
          }
        });
      }
    }, 2000);
  }

  // Vérifie si un numéro de compte fait partie de MES comptes
  estMonCompte(numeroCompte: string): boolean {
    return this.mesComptes.some(compte => 
      compte.numero === numeroCompte || 
      compte.numeroCompte === numeroCompte
    );
  }

  // Détermine le type de transaction pour l'affichage
  getTypeTransaction(transaction: any): string {
    if (transaction.type === 'DEPOT') {
      return 'depot';
    } else if (transaction.type === 'RETRAIT') {
      return 'retrait';
    } else if (transaction.type === 'VIREMENT') {
      // Virement émis : je suis le propriétaire source
      if (this.estMonCompte(transaction.numeroCompteSource)) {
        return 'virement-emis';
      }
      // Virement reçu : je suis le propriétaire destination
      if (this.estMonCompte(transaction.numeroCompteDestination)) {
        return 'virement-recus';
      }
    }
    return 'autre';
  }

  // Message personnalisé selon le type
  getMessageTransaction(transaction: any): string {
    const type = this.getTypeTransaction(transaction);
    
    switch(type) {
      case 'depot':
        return 'Vous avez effectué un dépôt';
      case 'retrait':
        return 'Vous avez effectué un retrait';
      case 'virement-emis':
        return `Vous avez transféré à ${transaction.proprietaireDestination}`;
      case 'virement-recus':
        return `Vous avez reçu de ${transaction.proprietaireSource}`;
      default:
        return 'Transaction';
    }
  }
}