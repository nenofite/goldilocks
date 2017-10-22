**Try Goldilocks by texting 619-332-0085!**

## Inspiration

Many fellow hackers were a little too cold at the hackathon. Blankets became a form of currency on the Slack chat. This inspired us to create a crowd-sourced thermostat where organizers of events could adjust the thermostat in realtime, without needing any special equipment or requiring attendees to download an app.

## What it does

Goldilocks uses a conversational UI powered by Twilio. Attendees text simple commands such as "too hot" or "too cold", and Goldilocks handles the rest. Event organizers can see in real time how the room is feeling.

## How we built it

We used Twilio to send SMS texts, Java and Spark to program the server, and SQLite to store the data. For our IDE we used IntelliJ.

## Challenges we ran into

Twilio banned us. (All the people making accounts from the same IP address at SD Hacks caused their fraud filters to go off, but we contacted them and they restored our account.)

Neither of us had made conversational UIs before, so it was a new challenge to learn how to convey information to users completely through dialogue. We had to simplify many of our interactions so that they could occur over text.

## Accomplishments that we're proud of

It was our first MLH hackathon! We didn't know what to expect. 

## What we learned

How to use Twilio, Spark, etc.

## What's next for Goldilocks

Adding real-time alerts when trends emerge in attendee's votes. Polishing the app.
