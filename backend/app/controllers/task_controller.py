from fastapi import HTTPException
from app.models import task as task_model
from typing import Optional

def get_all_tasks(user_id: int):
    return task_model.get_tasks_by_user_id(user_id)

def add_new_task(title: str, description: str, user_id: int, priority: str, deadline: Optional[str]):
    if not title:
        raise HTTPException(status_code=400, detail="Title cannot be empty")
    return task_model.create_task(title, description, user_id, priority, deadline)

def update_existing_task(task_id: int, title: str, description: str, is_completed: bool, user_id: int, priority: str, deadline: Optional[str]):
    if not title:
        raise HTTPException(status_code=400, detail="Title cannot be empty")
    success = task_model.update_task_by_id(task_id, title, description, is_completed, user_id, priority, deadline)
    if not success:
        raise HTTPException(status_code=404, detail="Task not found or you don't have permission")
    return {"message": "Task updated successfully"}

def remove_task(task_id: int, user_id: int):
    success = task_model.delete_task_by_id(task_id, user_id)
    if not success:
        raise HTTPException(status_code=404, detail="Task not found or you don't have permission")
    return {"message": "Task deleted successfully"}

def update_task_time(task_id: int, user_id: int, seconds: int):
    success = task_model.update_time_spent(task_id, user_id, seconds)
    if not success:
        raise HTTPException(status_code=404, detail="Task not found or you don't have permission")
    return {"message": "Time updated successfully"}

def get_productivity_stats(user_id: int):
    """Fetches productivity statistics from the model."""
    stats = task_model.get_productivity_stats_by_user_id(user_id)
    if stats is None:
        raise HTTPException(status_code=404, detail="Could not retrieve stats for user")
    return stats

def get_daily_summary(user_id: int):
    """Fetches today's productivity summary from the model."""
    summary = task_model.get_daily_summary_by_user_id(user_id)
    return summary