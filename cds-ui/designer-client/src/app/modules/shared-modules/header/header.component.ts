import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.css']
})
export class HeaderComponent implements OnInit {

  constructor(private router: Router) { }

  ngOnInit() {
  }

  skipToMain() {
    const main = document.getElementById('main-content');
    if (main) {
      main.focus();
    }
  }

  // redirect() {
  //   this.router.navigate(['/packages']);
  // }
}
