import { Component } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

interface Pet {
  id: number;
  name: string;
  age: number;
  description: string;
}

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent {
  title = 'front';
  pets: Pet[] = [];

  constructor(private http: HttpClient) {
    this.getAllPets().subscribe(data => this.pets = data);
  }

  getAllPets(): Observable<Pet[]> {
    return this.http.get<Pet[]>('/api/pets');
  }

  createPet(name: string, age: number, description: string): void {
    const formData = new FormData();
    formData.append('name', name);
    formData.append('age', age.toString());
    formData.append('description', description);

    this.http.post<Pet>('/api/pets', formData).subscribe(
      data => {
        this.pets.push(data);
      },
      error => {
        console.log(error);
      }
    );
  }

  deletePet(id: number): void {
    this.http.delete(`/api/pets/${id}`).subscribe(
      () => this.pets = this.pets.filter(pet => pet.id !== id)
    );
  }
}
