import { Component, OnInit, ViewChild } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { TestCase, TestStep, TestStepAttachment } from '../interfaces/models';
import { Location } from '@angular/common';
import { MatTableDataSource } from '@angular/material/table';
import { MatPaginator } from '@angular/material/paginator';
import { TestCaseService } from '../services/api';

@Component({
  selector: 'app-test-case-details',
  templateUrl: './test-case-details.component.html',
  styleUrls: ['./test-case-details.component.css']
})
export class TestCaseDetailsComponent implements OnInit {

  testCaseId: number;
  testCase: TestCase;
  attachments : TestStepAttachment[] = [];
  displayedColumns: string[] = ['position', 'description', 'object','operation','status','attachments'];
  dataSource: MatTableDataSource<TestStep>;

  @ViewChild(MatPaginator, {static: true}) paginator: MatPaginator;
  
  constructor(private testCaseService: TestCaseService, private route: ActivatedRoute, private location: Location ) { }

  ngOnInit(): void {
    this.route.paramMap.subscribe(params => {
      this.testCaseId = +params.get('testCaseId');
    });
    this.testCaseService.getTestCaseById(this.testCaseId, 'response')
    .subscribe(res => {
      if (res.status === 200) {
        this.testCase = res.body;
        this.dataSource = new MatTableDataSource<TestStep>(this.testCase.testSteps);
        this.dataSource.paginator = this.paginator;
        this.testCase.testSteps.forEach(step => {
          step.attachments.forEach(a => this.attachments.push(a));
        })
      }
    });
  }

  public goBack() {
    this.location.back();
  }

}
