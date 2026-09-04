import { Component, OnInit, ViewChild } from '@angular/core';
import { Environment } from '../interfaces/models';
import { FormGroup, FormBuilder } from '@angular/forms';
import { MatTableDataSource } from '@angular/material/table';
import { MatPaginator } from '@angular/material/paginator';
import { MatSort } from '@angular/material/sort';
import { EnvironmentService } from '../services/api';
import { filter } from 'rxjs/operators';

@Component({
  selector: 'app-environment-list',
  templateUrl: './environment-list.component.html',
  styleUrls: ['./environment-list.component.css']
})
export class EnvironmentListComponent implements OnInit {
  environments: Environment[];
  environmentForm: FormGroup;
  displayApplicationForm: Boolean = false;
  ackUserAction: Boolean = false;
  isSubmissionInProgress: Boolean = false;
  displayedColumns: string[] = ['position', 'name', 'description'];
  dataSource: MatTableDataSource<Environment>;

  @ViewChild(MatPaginator, {static: true}) paginator: MatPaginator;
  @ViewChild(MatSort, {static: true}) sort: MatSort;

  constructor(private environmentService: EnvironmentService,
    private formBuilder: FormBuilder) { 
    this.environmentForm = this.formBuilder.group({
      name: '',
      description: ''
    })
  }

  ngOnInit(): void {
      this.environmentService.getAllEnvironments('response')
      .pipe(
        filter(res => res.status === 200)
      )
      .subscribe(res => {
        this.environments = res.body;
        this.dataSource = new MatTableDataSource<Environment>(res.body);
        this.dataSource.paginator = this.paginator;
        this.dataSource.sort = this.sort;
      })
  }
  onSubmit() {
    this.isSubmissionInProgress = true;
    this.environmentService.createEnvironment(this.environmentForm.value,'response')
    .subscribe(res => {
      if (res.status === 201) {
        this.ackUserAction = true;
    setTimeout(() => {
      this.ackUserAction = false;
    },2000);
    this.environmentForm.reset();
      }
      this.isSubmissionInProgress = false;
    })
  }

  toggleApplicationForm() {
    this.environmentForm.reset();
    this.displayApplicationForm = !this.displayApplicationForm;
  }

}
