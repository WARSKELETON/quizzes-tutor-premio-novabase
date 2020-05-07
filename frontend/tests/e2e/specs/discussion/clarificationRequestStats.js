describe('Clarification Request Stats walkthrough (Make sure there are no previous ' +
  'clarifications made by demo student before running this test)', () => {
  beforeEach(() => {
    cy.demoStudentLogin()
  })

  afterEach(() => {
    cy.contains('Logout').click()
  })

  it('student login creates two Clarification Requests, teacher makes them public and student checks stats', () => {
    var content1 = generateContent(6)
    var content2 = generateContent(6)

    cy.solveQuiz()
    cy.createClarificationRequest(content1)
    cy.wait(1000)
    cy.solveQuiz()
    cy.createClarificationRequest(content2)

    cy.demoTeacherLogin()
    cy.goToDiscussion()
    cy.get('[data-cy="Search"]').type(content1)
    cy.contains(content1).click({force: true})
    cy.get('[data-cy="ButtonToPublic"]').click({force: true})
    cy.goToDiscussion()
    cy.get('[data-cy="Search"]').type(content2)
    cy.contains(content2).click({force: true})
    cy.get('[data-cy="ButtonToPublic"]').click({force: true})

    cy.demoStudentLogin()
    cy.contains('Stats').click()
    cy.get('[data-cy="totalClarificationRequests"]').contains('2').should('exist')
    cy.get('[data-cy="totalPublicClarificationRequests"]').contains('2').should('exist')
  });

});

function generateContent(length) {
  let result           = '';
  let characters       = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
  let charactersLength = characters.length;
  for (let i = 0; i < length; i++) {
    result += characters.charAt(Math.floor(Math.random() * charactersLength));
  }
  return result;
}