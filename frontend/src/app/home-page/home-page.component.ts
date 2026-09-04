import { Component, OnInit } from '@angular/core';
import { FormControl, Validators, FormGroup, FormBuilder } from '@angular/forms';
import { ApplicationService, EnvironmentService } from '../services/api';
import { Application, Environment } from '../interfaces/models';
import { filter } from 'rxjs/operators';
import { Router } from '@angular/router';

@Component({
  selector: 'app-home-page',
  templateUrl: './home-page.component.html',
  styleUrls: ['./home-page.component.css']
})
export class HomePageComponent implements OnInit {

  applications: Application[] = [];
  environments: Environment[] = [];
  searchForm: FormGroup;

  constructor(private formBuilder: FormBuilder, private router: Router, private applicationService: ApplicationService, private environmentService: EnvironmentService) {
      this.searchForm = this.formBuilder.group({
        application: '',
        environment: ''
      });

   }

  ngOnInit(): void {
    this.applicationService.getAllApplications('response')
    .pipe(
      filter(res => res.status === 200)
    ).subscribe( res => {
      this.applications = res.body;
    });
    this.environmentService.getAllEnvironments('response')
    .pipe(
      filter(res => res.status === 200)
    ).subscribe(res => {
      this.environments = res.body;
    })
  }

  onSubmit(searchForm){
    if (searchForm.application === '')
      return;
     this.router.navigate(['/applications/'+searchForm.application]);
  }
}
