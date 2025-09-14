#!/bin/bash

echo "🛑 Stopping all services..."
docker-compose down

echo "✅ Services stopped."
echo ""
echo "💡 To remove all data volumes as well:"
echo "   docker-compose down -v"
echo ""
echo "🗂️  To clean up Docker resources:"
echo "   docker system prune -f"
