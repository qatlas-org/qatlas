import { Component, OnInit, ViewChild } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Location } from '@angular/common';
import { TestExecution } from '../interfaces/testExecution';
import { MatTableDataSource } from '@angular/material/table';
import { MatPaginator } from '@angular/material/paginator';
import { ApplicationService } from '../services/api';

@Component({
  selector: 'app-application-details',
  templateUrl: './application-details.component.html',
  styleUrls: ['./application-details.component.css']
})
export class ApplicationDetailsComponent implements OnInit {

  appId: number;
  executions: TestExecution[];
  selectedExecution: TestExecution;
  displayedColumns: string[] = ['position', 'name', 'environment','browser','systemName','os','executedBy','status'];
  dataSource: MatTableDataSource<TestExecution>;

  @ViewChild(MatPaginator, {static: true}) paginator: MatPaginator;

  constructor(private route: ActivatedRoute, private applicationService: ApplicationService, private location: Location) { }

  ngOnInit(): void {
    this.route.paramMap.subscribe(params => {
      this.appId = +params.get('appId');
    });
    this.applicationService.getTestExecutions(this.appId,'response')
    .subscribe(res => {
      if (res.status === 200) {
        this.executions = res.body;
        this.executions.sort((a,b) => a.id - b.id);
        this.selectedExecution = this.executions[this.executions.length-1];
        this.dataSource = new MatTableDataSource<TestExecution>(res.body);
        this.dataSource.paginator = this.paginator;
      }
    })
  }

  public selectExecution(id: number){
    this.selectedExecution = this.executions.find(e => e.id === id);
  }

  public goBack() {
    this.location.back();
  }
}
