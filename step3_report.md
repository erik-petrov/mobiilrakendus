## Step 2 Report: API integration

## **Which API was chosen and why**
The API chosen is Joke API, because it provides us with necessary functionality and plays out the best with our app theme choice.
## **Example API endpoint used**
GET /joke provides the only endpoint that uses path and URL parameters to narrow down the search.
## **Error handling strategy**
Our API has an error field with each request, that is "true" when there is an error, to which our app will try again and otherwise fallback to a default joke.