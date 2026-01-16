from fastapi import FastAPI
from app.routes import auth_routes, task_routes, stats_routes, export_routes
from app.database.db import init_db

app = FastAPI()

@app.on_event("startup")
def on_startup():
    init_db()

app.include_router(auth_routes.router, prefix="/api/auth", tags=["Authentication"])
app.include_router(task_routes.router, prefix="/api/tasks", tags=["Tasks"])
app.include_router(stats_routes.router, prefix="/api/stats", tags=["Statistics"])

app.include_router(export_routes.router, prefix="/api/export", tags=["Export"])