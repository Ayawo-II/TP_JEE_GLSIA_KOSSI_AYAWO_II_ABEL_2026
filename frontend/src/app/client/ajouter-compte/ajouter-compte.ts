import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { ClientService } from '../../services/client-service';
import { LoginService } from '../../services/login/login-service';

@Component({
  selector: 'app-ajouter-compte',
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './ajouter-compte.html',
  styleUrl: './ajouter-compte.scss',
})
export class AjouterCompte implements OnInit {
  
  private clientService = inject(ClientService);
  private loginService = inject(LoginService);
  private router = inject(Router);
  
  // Données du formulaire
  compteData = {
    typeCompte: 'COURANT',
    soldeInitial: 0,
    clientId: 0
  };
  
  // États
  loading = false;
  message = '';
  isError = false;
  showConfirmation = false; // Nouveau: boîte de dialogue
  typesCompte = ['COURANT', 'EPARGNE'];
  user: any = null;

  ngOnInit(): void {
    setTimeout(() => {
      this.user = this.loginService.user();
      if (this.user) {
        this.compteData.clientId = this.user.clientId;
      }
    }, 1000);
  }

  // Étape 1: Préparation
  preparerCreation(): void {
    if (!this.isFormValid()) {
      this.message = 'Veuillez sélectionner un type de compte';
      this.isError = true;
      return;
    }
    
    // Affiche la boîte de confirmation
    this.showConfirmation = true;
  }

  // Étape 2: Confirmation
  confirmerCreation(): void {
    this.showConfirmation = false;
    this.creerCompte();
  }

  // Étape 3: Annulation
  annulerCreation(): void {
    this.showConfirmation = false;
  }

  // Étape 4: Création réelle
  private creerCompte(): void {
    this.loading = true;
    this.message = '';
    this.isError = false;
    
    this.clientService.ajouterCompte(this.compteData).subscribe({
      next: (response) => {
        this.loading = false;
        this.isError = false;
        this.message = 'Compte créé avec succès !';
        
        setTimeout(() => {
          this.router.navigate(['/client/comptes']);
        }, 2000);
      },
      error: (error) => {
        this.loading = false;
        this.isError = true;
        this.message = error.error?.message || 'Erreur lors de la création du compte';
      }
    });
  }

  isFormValid(): boolean {
    return this.compteData.clientId > 0 && 
           this.compteData.typeCompte.length > 0;
  }
}