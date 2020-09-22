for i in {0..100}
do
  curl "localhost:8080/sync" &
  pids[${i}]=$!
done

# wait for all pids
for pid in ${pids[*]}; do
    wait $pid
done


