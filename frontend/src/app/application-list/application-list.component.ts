import { Component, OnInit, ViewChild } from '@angular/core';
import { filter } from 'rxjs/operators'
import { Application } from '../interfaces/application';
import { FormBuilder, FormGroup } from '@angular/forms';
import { MatTableDataSource } from '@angular/material/table';
import { MatPaginator } from '@angular/material/paginator';
import { MatSort } from '@angular/material/sort';
import { ApplicationService } from '../services/api';

@Component({
  selector: 'app-application-list',
  templateUrl: './application-list.component.html',
  styleUrls: ['./application-list.component.css']
})
export class ApplicationListComponent implements OnInit {
  applications: Application[];
  applicationVOs: Application[];
  applicationForm: FormGroup;
  displayApplicationForm: Boolean = false;
  ackUserAction: Boolean = false;
  isSubmissionInProgress: Boolean = false;
  displayedColumns: string[] = ['position', 'name', 'description'];
  dataSource: MatTableDataSource<Application>;

  @ViewChild(MatPaginator, {static: true}) paginator: MatPaginator;
  @ViewChild(MatSort, {static: true}) sort: MatSort;

  constructor(private appService: ApplicationService,
    private formBuilder: FormBuilder) { 
    this.applicationForm = this.formBuilder.group({
      name: '',
      description: ''
    })
  }

  ngOnInit(): void {
       this.appService.getAllApplications('response')
      .pipe(
        filter(res => res.status === 200)
      )
      .subscribe(res => {
        this.applicationVOs = res.body;
        this.dataSource = new MatTableDataSource<Application>(res.body);
        this.dataSource.paginator = this.paginator;
        this.dataSource.sort = this.sort;
      })
  }

  onSubmitApplication() {
    this.isSubmissionInProgress = true;
    this.appService.createApplication(this.applicationForm.value,'response')
    .subscribe(res => {
      if (res.status === 201) {
        this.ackUserAction = true;
    setTimeout(() => {
      this.ackUserAction = false;
    },2000);
    this.applicationForm.reset();
      }
      this.isSubmissionInProgress = false;
    })
  }

  toggleApplicationForm() {
    this.applicationForm.reset();
    this.displayApplicationForm = !this.displayApplicationForm;
  }
  
}