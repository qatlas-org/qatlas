import { Component, OnInit, ViewChild } from '@angular/core';
import { TestCase } from '../interfaces/models';
import { ActivatedRoute } from '@angular/router';
import { Location } from '@angular/common';
import { MatTableDataSource } from '@angular/material/table';
import { MatPaginator } from '@angular/material/paginator';
import { TestExecutionService } from '../services/api';

@Component({
  selector: 'app-test-case',
  templateUrl: './test-case.component.html',
  styleUrls: ['./test-case.component.css']
})
export class TestCaseComponent implements OnInit {

  testCases: TestCase[];
  executionId: number;
  status : string [] = [];
  selectedStatus: string;
  displayedColumns: string[] = ['position', 'suiteName', 'executionTime','totalSteps','passedSteps','failedSteps','warningSteps'];
  dataSource: MatTableDataSource<TestCase>;

  @ViewChild(MatPaginator, {static: true}) paginator: MatPaginator;

  constructor(private testExecutionService: TestExecutionService, private route: ActivatedRoute, private location: Location) { }

  ngOnInit(): void {
    this.route.paramMap.subscribe(params => {
      this.executionId = +params.get('executionId');
      this.status.push(params.get('testCaseStatus'));
      this.selectedStatus = params.get('testCaseStatus');
    });
    this.testExecutionService.getTestExecutionTestCases(this.executionId,this.status,'response')
    .subscribe(res => {
      if (res.status === 200) {
        this.testCases = res.body;
        this.dataSource = new MatTableDataSource<TestCase>(res.body);
        this.dataSource.paginator = this.paginator;
      }
    })
  }
  public goBack() {
    this.location.back();
  }
}
