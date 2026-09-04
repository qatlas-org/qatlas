import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { ApplicationListComponent } from './application-list/application-list.component';
import { ApplicationDetailsComponent } from './application-details/application-details.component';
import { TestCaseComponent } from './test-case/test-case.component';
import { TestCaseDetailsComponent } from './test-case-details/test-case-details.component';
import { EnvironmentListComponent } from './environment-list/environment-list.component';
import { HomePageComponent } from './home-page/home-page.component';


const routes: Routes = [
  { path: '', component: HomePageComponent},
  { path: 'applications', component: ApplicationListComponent},
  { path: 'applications/:appId', component: ApplicationDetailsComponent},
  { path: 'environments', component: EnvironmentListComponent},
  { path: 'execution/:executionId/:testCaseStatus', component: TestCaseComponent},
  { path: 'testCase/:testCaseId', component: TestCaseDetailsComponent}
 ];

@NgModule({
  imports: [RouterModule.forRoot(routes,{
    useHash: true
  })],
  exports: [RouterModule]
})
export class AppRoutingModule { }
