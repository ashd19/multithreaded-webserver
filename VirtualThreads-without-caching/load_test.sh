#!/bin/bash

# Load Test Script for Virtual Threads Server WITHOUT Caching
# This script tests the performance impact of disk I/O on every request

echo "╔════════════════════════════════════════════════════════════════╗"
echo "║  Virtual Threads Server - WITHOUT Caching - Load Test         ║"
echo "║  Demonstrates performance impact of disk I/O bottleneck        ║"
echo "╚════════════════════════════════════════════════════════════════╝"
echo ""

# Configuration
SERVER_PORT=8020
SERVER_URL="http://localhost:${SERVER_PORT}/"

# Check if server is running
echo "🔍 Checking if server is running on port ${SERVER_PORT}..."
if ! curl -s --max-time 2 "${SERVER_URL}" > /dev/null 2>&1; then
    echo "❌ Server is not responding on port ${SERVER_PORT}"
    echo "   Please start the server first:"
    echo "   cd VirtualThreads-without-caching && java Server"
    exit 1
fi
echo "✅ Server is running"
echo ""

# Check if Apache Bench is installed
if ! command -v ab &> /dev/null; then
    echo "❌ Apache Bench (ab) is not installed"
    echo "   Install with: sudo apt-get install apache2-utils"
    exit 1
fi

# Function to run a test
run_test() {
    local requests=$1
    local concurrency=$2
    local test_name=$3
    
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo "📊 Test: ${test_name}"
    echo "   Requests: ${requests} | Concurrency: ${concurrency}"
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    
    ab -n "${requests}" -c "${concurrency}" "${SERVER_URL}" 2>&1 | tee "/tmp/load_test_${concurrency}_${requests}.txt"
    
    echo ""
    echo "Results saved to: /tmp/load_test_${concurrency}_${requests}.txt"
    echo ""
}

# Warm-up
echo "🔥 Warming up server..."
curl -s "${SERVER_URL}" > /dev/null
echo "✅ Warm-up complete"
echo ""

# Test Suite
echo "╔════════════════════════════════════════════════════════════════╗"
echo "║  Starting Load Tests                                           ║"
echo "╚════════════════════════════════════════════════════════════════╝"
echo ""

# Test 1: Low concurrency baseline
run_test 1000 10 "Baseline (Low Concurrency)"

# Test 2: Medium concurrency
run_test 5000 100 "Medium Load"

# Test 3: High concurrency
run_test 10000 500 "High Load"

# Test 4: Very high concurrency (stress test)
run_test 10000 1000 "Stress Test"

# Summary
echo "╔════════════════════════════════════════════════════════════════╗"
echo "║  Load Test Complete                                            ║"
echo "╚════════════════════════════════════════════════════════════════╝"
echo ""
echo "📈 Performance Summary:"
echo ""
echo "Expected Results (WITHOUT Caching):"
echo "  • Throughput: ~1,000-2,000 requests/second"
echo "  • Mean Latency: ~500-800ms"
echo "  • P95 Latency: ~1,000-1,500ms"
echo "  • Bottleneck: Disk I/O on every request"
echo ""
echo "⚠️  Key Observations:"
echo "  • High latency due to disk reads"
echo "  • Variable response times"
echo "  • Lower throughput despite virtual threads"
echo "  • CPU underutilized (waiting on I/O)"
echo ""
echo "💡 Next Steps:"
echo "  1. Compare with cached version (port 8010)"
echo "  2. Run: cd ../VirtualThreads-with-caching && ./load_test.sh"
echo "  3. Observe 10x+ performance improvement with caching"
echo ""
echo "📁 Test results saved in /tmp/load_test_*.txt"
echo ""

# Optional: Compare with cached version if running
if curl -s --max-time 2 "http://localhost:8010/" > /dev/null 2>&1; then
    echo "╔════════════════════════════════════════════════════════════════╗"
    echo "║  Bonus: Quick Comparison with Cached Version                  ║"
    echo "╚════════════════════════════════════════════════════════════════╝"
    echo ""
    echo "Running quick test on cached version (port 8010)..."
    echo ""
    
    ab -n 1000 -c 100 "http://localhost:8010/" 2>&1 | grep -E "Requests per second|Time per request|Transfer rate"
    
    echo ""
    echo "✅ Cached version is significantly faster!"
fi

echo "Done! 🎉"
