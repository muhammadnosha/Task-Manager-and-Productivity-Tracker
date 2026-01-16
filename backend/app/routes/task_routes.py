from fastapi import APIRouter
from pydantic import BaseModel
from typing import Optional
from app.controllers import task_controller

router = APIRouter()

class TaskBase(BaseModel):
    title: str
    description: Optional[str] = None
    priority: Optional[str] = 'Medium'
    deadline: Optional[str] = None

class TaskCreate(TaskBase):
    user_id: int

class TaskUpdate(TaskBase):
    is_completed: bool
    user_id: int

class TaskDelete(BaseModel):
    user_id: int

class TimeUpdateRequest(BaseModel):
    user_id: int
    time_spent_seconds: int

@router.get("/{user_id}")
def get_tasks(user_id: int):
    return task_controller.get_all_tasks(user_id)

@router.post("/")
def create_task(task: TaskCreate):
    return task_controller.add_new_task(task.title, task.description, task.user_id, task.priority, task.deadline)

@router.put("/{task_id}")
def update_task(task_id: int, task: TaskUpdate):
    return task_controller.update_existing_task(task_id, task.title, task.description, task.is_completed, task.user_id, task.priority, task.deadline)

@router.delete("/{task_id}")
def delete_task(task_id: int, body: TaskDelete):
    return task_controller.remove_task(task_id, body.user_id)

# --- NEW ENDPOINT ---
@router.patch("/{task_id}/time")
def update_time(task_id: int, request: TimeUpdateRequest):
    return task_controller.update_task_time(task_id, request.user_id, request.time_spent_seconds)