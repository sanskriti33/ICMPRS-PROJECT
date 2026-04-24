# Public Deployment

This project can now be deployed as a public browser app.

## What Was Prepared

- `WebServer.java` now reads the hosting port from the `PORT` environment variable.
- `render.yaml` was added for easy Render deployment.
- Existing database settings already support environment variables:
  - `ICMPRS_DB_URL`
  - `ICMPRS_DB_USER`
  - `ICMPRS_DB_PASSWORD`

## Public Hosting Option

Recommended: Render

Official docs:
- https://render.com/docs/web-services
- https://render.com/docs/blueprint-spec

## Steps

1. Upload this project to GitHub.
2. Create a Render account and connect the GitHub repo.
3. Create a new Web Service or Blueprint from the repo.
4. Set these environment variables in Render:
   - `ICMPRS_DB_URL`
   - `ICMPRS_DB_USER`
   - `ICMPRS_DB_PASSWORD`
5. Deploy.
6. Render will give a public link such as:
   - `https://your-app-name.onrender.com`

## Important

Your current local MySQL database on your laptop is not publicly accessible.

To make the deployed app work for everyone, use an online MySQL database or update the app to use file storage in production.
