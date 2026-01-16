from typing import Optional
from app.database.db import get_db_connection

def get_tasks_by_user_id(user_id: int):
    """Fetches all tasks for a specific user."""
    conn = get_db_connection()
    tasks = conn.execute("SELECT * FROM tasks WHERE user_id = ?", (user_id,)).fetchall()
    conn.close()
    return [dict(task) for task in tasks]

def create_task(title: str, description: str, user_id: int, priority: str, deadline: Optional[str]):
    """Creates a new task in the database."""
    conn = get_db_connection()
    cursor = conn.cursor()
    cursor.execute(
        "INSERT INTO tasks (title, description, user_id, priority, deadline) VALUES (?, ?, ?, ?, ?)",
        (title, description, user_id, priority, deadline)
    )
    conn.commit()
    new_task_id = cursor.lastrowid
    new_task = conn.execute("SELECT * FROM tasks WHERE id = ?", (new_task_id,)).fetchone()
    conn.close()
    return dict(new_task) if new_task else None

def update_task_by_id(task_id: int, title: str, description: str, is_completed: bool, user_id: int, priority: str, deadline: Optional[str]):
    """
    Updates an existing task. If the task is being marked as complete,
    it also sets the completion_date to the current date.
    """
    conn = get_db_connection()
    cursor = conn.cursor()

    query = """
        UPDATE tasks
        SET
            title = ?,
            description = ?,
            is_completed = ?,
            priority = ?,
            deadline = ?,
            completion_date = CASE
                WHEN ? = 1 THEN date('now')
                ELSE completion_date
            END
        WHERE id = ? AND user_id = ?
    """
    params = (title, description, is_completed, priority, deadline, is_completed, task_id, user_id)

    cursor.execute(query, params)
    conn.commit()
    updated_rows = cursor.rowcount
    conn.close()
    return updated_rows > 0

def delete_task_by_id(task_id: int, user_id: int):
    """Deletes a task from the database."""
    conn = get_db_connection()
    cursor = conn.cursor()
    cursor.execute("DELETE FROM tasks WHERE id = ? AND user_id = ?", (task_id, user_id))
    conn.commit()
    deleted_rows = cursor.rowcount
    conn.close()
    return deleted_rows > 0

def update_time_spent(task_id: int, user_id: int, seconds: int):
    """Updates the time spent on a specific task."""
    conn = get_db_connection()
    cursor = conn.cursor()
    cursor.execute(
        "UPDATE tasks SET time_spent_seconds = ? WHERE id = ? AND user_id = ?",
        (seconds, task_id, user_id)
    )
    conn.commit()
    updated_rows = cursor.rowcount
    conn.close()
    return updated_rows > 0

def get_productivity_stats_by_user_id(user_id: int):
    """
    Retrieves and aggregates productivity data for a user over the last 30 days.
    It groups completed tasks by their completion date and sums the time spent.
    """
    conn = get_db_connection()
    query = """
        SELECT
            completion_date as date,
            SUM(time_spent_seconds) as total_seconds
        FROM tasks
        WHERE user_id = ?
          AND is_completed = 1
          AND completion_date >= date('now', '-30 days')
        GROUP BY completion_date
        ORDER BY completion_date ASC;
    """
    stats = conn.execute(query, (user_id,)).fetchall()
    conn.close()
    return [dict(row) for row in stats]

def get_daily_summary_by_user_id(user_id: int):
    """
    Calculates the total time spent on tasks completed today for a specific user.
    """
    conn = get_db_connection()
    query = """
        SELECT SUM(time_spent_seconds) as total_seconds
        FROM tasks
        WHERE user_id = ?
          AND is_completed = 1
          AND completion_date = date('now');
    """
    result = conn.execute(query, (user_id,)).fetchone()
    conn.close()
    total_seconds = result['total_seconds'] if result and result['total_seconds'] is not None else 0
    return {"total_seconds": total_seconds}