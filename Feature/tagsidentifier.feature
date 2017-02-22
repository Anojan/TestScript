Feature: Identify the tags in pages

Background: 
	Given I go to straitstimes Home Page

@test
Scenario: The tags in the home page is same as the provided tags in the har file
	When I get the values from website
	And I compare the values with har entries
	Then I see the values are available as the same
	